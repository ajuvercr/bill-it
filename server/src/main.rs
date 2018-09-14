
#[macro_use]
extern crate futures;
extern crate tokio_core;
extern crate tokio_io;
extern crate tokio;

#[macro_use]
extern crate serde_derive;

extern crate serde;
extern crate serde_json;

extern crate core;

use std::env;
use std::net::SocketAddr;
use std::collections::HashMap;
use std::cell::RefCell;
use std::rc::Rc;

use futures::stream::{Stream};
use futures::sync::mpsc;


use tokio_core::net::{TcpListener};
use tokio_core::reactor::Core;

mod server;
use server::Server;
mod types;
use types::User;
mod events;
use events::{Event, Update, Get};

mod connection_handler;
use connection_handler::ConnectionHandler;

fn main() {
    let addr = env::args().nth(1).unwrap_or("127.0.0.1:8080".to_string());
    let server_addr = addr.parse::<SocketAddr>().unwrap();

    let mut core = Core::new().unwrap();
    let handle = core.handle();

    let socket = TcpListener::bind(&server_addr, &handle).unwrap();
    println!("Listening on: {}", server_addr);


    let (server_handle, rx) = mpsc::unbounded();
    let server = Server::new(rx);

    let conn_handler = ConnectionHandler::new(handle.clone(), server_handle);

    let done = socket.incoming().for_each(|(stream, _addr)| {
        conn_handler.new_connection(stream);
        Ok(())
    });

    let me: User = User::new();
    let event: Event = Event::Update(Update::User {user: me, id: "me".to_string()});

    let serialized = serde_json::to_string(&event).unwrap();
    println!("serialized: {}", serialized);

    let get = Get::User{user: "me".to_string()};
    println!("get: {}", serde_json::to_string(&get).unwrap());

    // let other_me_group: Group = serde_json::from_str(&serialized).unwrap();

    // println!("this is my group {:?}", me_group);
    // println!("this is my other group {:?}", other_me_group);

    //server.handle_event(Event::Update(Update::Group {group: me_group, id: "hallo".to_string()}));


    core.handle().spawn(server);
    core.run(done).unwrap();
}