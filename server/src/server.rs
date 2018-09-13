extern crate serde_json;

use futures::*;
use futures::Future;
use futures::sync::mpsc;
use futures::sync::mpsc::UnboundedReceiver;

use std;
use std::collections::HashMap;

use types::{User, Group, Type};

use events::{Event, Update, Get, ServerEvent};

pub type Id = String;

#[derive(Serialize, Deserialize, Debug, Clone)]
struct State {
    users: HashMap<Id, User>,
    groups: HashMap<Id, Group>,
}

impl State {
    fn new() -> State {
        State {
            users: HashMap::new(),
            groups: HashMap::new(),
        }
    }

    fn handle_update(&mut self, update: Update) -> Event {
        match update {
            Update::Group {group, id} => {
                //TODO: send update to removed user
                self.groups.insert(id.clone(), group.clone());
                Event::Notify(Type::Group(group))
            },

            Update::User {user, id} => {
                self.users.insert(id.clone(), user.clone());
                Event::Notify(Type::User(user))
            }
        }
    }
}

pub struct Server {
    state: State,
    connections: HashMap<Id, mpsc::UnboundedSender<Type>>,
    handle: UnboundedReceiver<(ServerEvent, Id)>
}

impl Future for Server {
    type Item = ();
    type Error = ();

    fn poll(&mut self) -> Result<Async<()>, ()> {
        loop {
            if let Some((e, id)) = try_ready!(self.handle.poll()) {
                self.handle_server_event(e, id);
            }
        }
    }
}

impl Server {
    pub fn new(
            handle: UnboundedReceiver<(ServerEvent, Id)>) -> Server {
        let mut server = Server {
            state: State::new(),
            connections: HashMap::new(),
            handle,
        };

        match server.load(&"db.json".to_string()) {
            Ok(_) => println!("state loaded"),
            Err(_) => println!("couldn't load state"),
        }

        server
    }

    pub fn save(&self, location: &str) -> std::io::Result<()> {
        use std::fs::File;
        use std::io::Write;

        let mut file = File::create(location)?;
        let serialized_state = serde_json::to_string(&self.state.clone()).unwrap();

        file.write_all(serialized_state.as_bytes())?;
        Ok(()) 
    }

    pub fn load(&mut self, location: &str) -> std::io::Result<()> {
        use std::fs::File;
        use std::io::Read;

        let mut file = File::open(location)?;
        let mut serialized_state = String::new();
        file.read_to_string(&mut serialized_state)?;
        self.state = serde_json::from_str(&serialized_state).unwrap();

        Ok(())
    }

    fn handle_server_event(&mut self, event: ServerEvent, id: Id) {
        match event {
            ServerEvent::Connect(sender) => {
                self.connections.insert(id.clone(), sender); 
                self.handle_event(Event::Get(Get::User{user: id.clone()}), id)
            },
            ServerEvent::Disconnect => {self.connections.remove(&id);},
            ServerEvent::Event(event) => self.handle_event(event, id),
        }
    }

    fn handle_event(&mut self, event: Event, id: Id) {
        match event {
            Event::Update(update) => {
                let event = self.state.handle_update(update);

                // unhandled result
                self.save(&"db.json".to_string()).unwrap();
                self.handle_event(event, id);
            },
            Event::Get(Get::User{ user }) => {
                if let Some(mut sender) = self.connections.get(&id){
                    if let Some(user) = self.state.users.get(&user) {
                        println!("sending {:?}", user);
                        sender.unbounded_send(Type::User(user.clone())).unwrap();
                    } else {
                        println!("didnt find user {:?}", user);
                        sender.unbounded_send(Type::Error{error: format!("user with id {:?} not found", user)}).unwrap();
                    }
                }

                println!("getting user {:?}", user);
            },
            Event::Get(Get::Group{ group }) => {
                if let Some(mut sender) = self.connections.get(&id){
                    if let Some(group) = self.state.groups.get(&group) {
                        sender.unbounded_send(Type::Group(group.clone())).unwrap();
                    } else {
                        sender.unbounded_send(Type::Error{error: format!("group with id {:?} not found", group)}).unwrap();
                    }
                }

                println!("getting group {:?}", group);
            },
            Event::Notify(Type::User(user)) => {
                self.handle_event(Event::Get(Get::User{ user: user.id.clone()}), id.clone());
                user.friends.iter().for_each(|u_id| {
                    self.handle_event(
                        Event::Get(Get::User{ user: user.id.clone()}), 
                        u_id.clone()
                    );
                });

                user.groups.iter().for_each(|g_id| {
                    if self.state.groups.contains_key(g_id) {
                        let group = self.state.groups.get(g_id).unwrap().clone();
                        self.handle_event(
                            Event::Notify(Type::Group(group)), id.clone());
                    } else {
                        println!("shouldn't have happend, someone has invalid group with id {:?}", g_id);          
                    }
                });

                println!("notifying user: {:?}", user);
            },
            Event::Notify(Type::Group(group)) => {
                group.userIds.iter().for_each(|u_id| {
                    self.handle_event(
                        Event::Get(Get::Group{ group: group.id.clone()}), 
                        u_id.clone()
                    )
                });
                println!("notifying group: {:?}", group);
            },
            Event::Error {error} => {
                println!("Shit happend {}", error);
                if let Some(mut sender) = self.connections.get(&id) {
                    sender.unbounded_send(Type::Error{error}).unwrap();
                }
            },
            e => {
                println!("shouldn't happen, got event {:?}", e);
            }
        }
    }
}