extern crate serde_json;

use futures::*;
use futures::Future;
use futures::sync::mpsc;
use futures::sync::mpsc::UnboundedReceiver;

use std;
use std::collections::HashMap;
use std::cell::RefCell;
use std::rc::Rc;

use types::User;
use types::Group;

use events::{Event, Update, Get};

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
                self.groups.insert(id.clone(), group);
                Event::Notify {id: id}
            },

            Update::User {user, id} => {
                self.users.insert(id.clone(), user);
                Event::Notify {id: id}
            }
        }
    }
}

pub struct Server {
    state: State,
    connections: Rc<RefCell<HashMap<String, mpsc::UnboundedSender<Event>>>>,
    handle: UnboundedReceiver<Event>
}

impl Future for Server {
    type Item = ();
    type Error = ();

    fn poll(&mut self) -> Result<Async<()>, ()> {
        match try_ready!(self.handle.poll()) {
            Some(e) => {
                self.handle_event(e);
                Ok(Async::NotReady)
            },
            None    => Ok(Async::Ready(()))
        }
    }
}

impl Server {
    pub fn new(
            connections: Rc<RefCell<HashMap<String, mpsc::UnboundedSender<Event>>>>, 
            handle: UnboundedReceiver<Event>) -> Server {
        Server {
            state: State::new(),
            connections,
            handle,
        }
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

    pub fn handle_event(&mut self, event: Event) {
        match event {
            Event::Update(update) => {
                let event = self.state.handle_update(update);

                // unhandled result
                self.save(&"db.json".to_string()).unwrap();
                self.handle_event(event);
            },
            Event::Get(get) => {

                println!("getting {:?}", get);
            },
            Event::Notify {id} => {
                println!("notifying id: {}", id);
            },
            Event::Error {error} => {
                println!("Shit happend {}", error);
            }
        }
    }
}