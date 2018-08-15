
extern crate futures;
extern crate tokio_core;
extern crate tokio_io;

#[macro_use]
extern crate serde_derive;

extern crate serde;
extern crate serde_json;

use std::env;
use std::net::SocketAddr;

use futures::Future;
use futures::stream::Stream;
use tokio_io::AsyncRead;
use tokio_io::io::copy;
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
    let addr = addr.parse::<SocketAddr>().unwrap();

    let mut core = Core::new().unwrap();
    let handle = core.handle();

    let socket = TcpListener::bind(&addr, &handle).unwrap();
    println!("Listening on: {}", addr);
    let mut server = Server::new();

    let (done, mut server) = server.listen(socket, core.handle());

    let me: User = User::new();
    let mut me_group: Group = Group::new();
    me_group.add_user(me);

    let serialized = serde_json::to_string(&me_group).unwrap();
    println!("serialized: {}", serialized);

    let other_me_group: Group = serde_json::from_str(&serialized).unwrap();

    println!("this is my group {:?}", me_group);
    println!("this is my other group {:?}", other_me_group);

    server.handle_event(Event::Update(Update::Group {group: me_group, id: "hallo".to_string()}));
    if let Err(e) = server.save("db.json"){
        println!("couldn't save: {:?}", e);
    }

    core.run(done).unwrap();
}