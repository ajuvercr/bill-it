
extern crate futures;
extern crate tokio_core;
extern crate tokio_io;

#[macro_use]
extern crate serde_derive;

extern crate serde;
extern crate serde_json;

use std::env;
use std::net::SocketAddr;
use std::io::{Error, ErrorKind, BufReader};
use std::iter;
use std::collections::HashMap;
use std::cell::RefCell;
use std::rc::Rc;

use futures::Future;
use futures::stream::{self, Stream};
use futures::sync::mpsc;
use tokio_io::io;


use tokio_core::net::TcpListener;
use tokio_core::reactor::Core;

mod server;
use server::Server;
mod types;
use types::User;
use types::Group;
mod events;
use events::{Event, Update};

fn main() {
    let addr = env::args().nth(1).unwrap_or("127.0.0.1:8080".to_string());
    let server_addr = addr.parse::<SocketAddr>().unwrap();

    let mut core = Core::new().unwrap();
    let handle = core.handle();

    let socket = TcpListener::bind(&server_addr, &handle).unwrap();
    println!("Listening on: {}", server_addr);

    let connections = Rc::new(RefCell::new(HashMap::new()));
    let (server_handle, rx) = mpsc::unbounded();
    connections.borrow_mut().insert(server_addr, server_handle);
    let mut server = Server::new(connections.clone(), rx);

    let done = socket.incoming().for_each(move |(stream, addr)| {
        use tokio_io::AsyncRead;
        println!("new connection! {:?}", addr);
        let (reader, writer) = stream.split();

        let (tx, rx) = mpsc::unbounded();
        connections.borrow_mut().insert(addr, tx);

        let connection_inner = connections.clone();
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
                println!("got event: {:?}", event);
                let tx = if event.is_error() { 
                    conns.get_mut(&addr).unwrap()
                } else {
                    conns.get_mut(&server_addr).unwrap()
                };
                tx.unbounded_send(event).unwrap();
                reader
            })
        }); // return socket_reader

        let socket_writer = rx.fold(writer, |writer, event| {
            let amt = io::write_all(writer, event.into_bytes());
            let amt = amt.map(|(writer, _)| writer);
            amt.map_err(|_| ())
        });

        let connections = connections.clone();
        let socket_reader = socket_reader.map_err(|_: std::io::Error| ());
        let connection = socket_reader.map(|_| ()).select(socket_writer.map(|_| ()));
        handle.spawn(connection.then(move |_| {
            connections.borrow_mut().remove(&addr);
            println!("Connection {} closed.", addr);
            Ok(())
        }));

        Ok(())
    });

    let me: User = User::new();
    let event: Event = Event::Update(Update::User {user: me, id: "me".to_string()});

    let serialized = serde_json::to_string(&event).unwrap();
    println!("serialized: {}", serialized);

    // let other_me_group: Group = serde_json::from_str(&serialized).unwrap();

    // println!("this is my group {:?}", me_group);
    // println!("this is my other group {:?}", other_me_group);

    //server.handle_event(Event::Update(Update::Group {group: me_group, id: "hallo".to_string()}));


    core.handle().spawn(server);
    core.run(done).unwrap();
}