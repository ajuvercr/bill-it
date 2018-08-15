extern crate serde_json;

use futures::*;
use futures::Future;
use futures::stream::{self, Stream};
use futures::sync::mpsc;
use tokio_core::reactor::Handle;
use futures::stream::ForEach;
use tokio_io::io;
use tokio_core::net::Incoming;
use tokio_core::net::TcpListener;

use std;
use std::io::{Error, ErrorKind, BufReader};
use std::iter;
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
    connections: Rc<RefCell<HashMap<std::net::SocketAddr, mpsc::UnboundedSender<Event>>>>,
}

impl Server {
    pub fn new() -> Server {
        Server {
            state: State::new(),
            connections: Rc::new(RefCell::new(HashMap::new()))
        }
    }

    pub fn listen(self, socket: TcpListener, handle: Handle) -> (impl Future<Item = (), Error = Error>, Server) {
        //let connections = connections.clone();
        (socket.incoming().for_each(move |(stream, addr)| {
            use tokio_io::AsyncRead;
            println!("new connection! {:?}", addr);
            let (reader, writer) = stream.split();

            let (tx, rx) = mpsc::unbounded();
            self.connections.borrow_mut().insert(addr, tx);

            let connection_inner = self.connections.clone();
            let reader = BufReader::new(reader);  

            let iter = stream::iter_ok(iter::repeat(()));
            let socket_reader = iter.fold(reader, move |reader, _| {
                let line = io::read_until(reader, b'\n', Vec::new());
                let line = line.and_then(|(reader, vec)|{
                    if vec.len() == 0 {
                        Err(Error::new(ErrorKind::BrokenPipe, "broken pipe"))
                    }else{
                        Ok((reader, vec))
                    }
                });

                let line = line.map(|(reader, message)| {
                    (reader, Event::from_bytes(message))
                });

                let connections = connection_inner.clone();

                line.map(move |(reader, event)| {
                    let mut conns = connections.borrow_mut();
                    if event.is_error() {
                        let tx = conns.get_mut(&addr).unwrap();
                        tx.unbounded_send(event);
                    }else{
                        self.handle_event(event);
                    }
                    reader
                })
            }); // return socket_reader

            let socket_writer = rx.fold(writer, |writer, event| {
                let amt = io::write_all(writer, event.into_bytes());
                let amt = amt.map(|(writer, _)| writer);
                amt.map_err(|_| ())
            });

            let connections = self.connections.clone();
            let socket_reader = socket_reader.map_err(|_: Error| ());
            let connection = socket_reader.map(|_| ()).select(socket_writer.map(|_| ()));
            handle.spawn(connection.then(move |_| {
                connections.borrow_mut().remove(&addr);
                println!("Connection {} closed.", addr);
                Ok(())
            }));

            Ok(())
        }), self)
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