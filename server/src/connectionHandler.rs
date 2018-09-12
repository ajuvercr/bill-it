
use std::net::SocketAddr;
use std::collections::HashMap;
use std::cell::RefCell;
use std::rc::Rc;
use std::io::{Error, ErrorKind, BufReader};
use std::mem;


use futures::sync::mpsc;
use futures::{Future, Async, Poll, Stream};

use tokio_core::net::{TcpListener, TcpStream};

use tokio_core::reactor::Handle;
use tokio_io::io;
use tokio_io::AsyncRead;
use tokio::io::ReadHalf;

use events::{Event};

#[derive(Clone)]
pub struct ConnectionHandler {
    handle: Handle,
    conns: Rc<RefCell<HashMap<String, mpsc::UnboundedSender<Event>>>>,
    server_addr: String
}

impl ConnectionHandler {
    pub fn new(handle: Handle, conns: Rc<RefCell<HashMap<String, mpsc::UnboundedSender<Event>>>>, server_addr: String) -> ConnectionHandler {
        ConnectionHandler {
            handle, conns, server_addr
        }
    }

    pub fn new_connection(&self, stream: TcpStream, addr: SocketAddr) {
        use tokio_io::AsyncRead;

        //connHandler.new_connection(stream.clone(), addr);


        println!("new connection! {:?}", addr);
        let (reader, writer) = stream.split();

        // let mut id = [0;4];
        // reader.poll_read(&mut id[..]).unwrap();
        // println!("new connection with id: {}", String::from_utf8(id.to_vec()).unwrap());
        let (tx, rx) = mpsc::unbounded::<Event>();

        let conn = Connection::new(tx, reader, self.conns.clone(), self.server_addr.clone());

        let socket_writer = rx.fold(writer, |writer, event| {
            let amt = io::write_all(writer, event.into_bytes());
            let amt = amt.map(|(writer, _)| writer);
            amt.map_err(|_| ())
        });

        let connections = self.conns.clone();
        let socket_reader = conn.map_err(|_| ());
        let connection = socket_reader.map(|_| ()).select(socket_writer.map(|_| ()));
        let s_addr = self.server_addr.clone();

        self.handle.spawn(connection.then(move |_| {
            connections.borrow_mut().remove(&s_addr);
            println!("disconnecting {:?}", s_addr);
            Ok(())
        }));
    }

}

enum State {
    GetID,
    Parsing,
}

struct Connection {
    conns: Rc<RefCell<HashMap<String, mpsc::UnboundedSender<Event>>>>,
    reader: ReadHalf<TcpStream>,
    id: Result<String, mpsc::UnboundedSender<Event>>,
    buf: Vec<u8>,
    state: State,
    server_addr: String,
}

impl Connection {
    fn new(sender: mpsc::UnboundedSender<Event>, reader: ReadHalf<TcpStream>, conns: Rc<RefCell<HashMap<String, mpsc::UnboundedSender<Event>>>>, server_addr: String) 
        -> Connection {
        Connection {
            conns, reader,
            id: Err(sender),
            buf: Vec::new(),
            state: State::GetID,
            server_addr,
        }
    }

    fn parse_id(&mut self, id_bytes: Vec<u8>) {
        let id = String::from_utf8(id_bytes).unwrap();
        let sender = mem::replace(&mut self.id, Ok(id.clone()));
        self.conns.borrow_mut().insert(id, sender.unwrap_err());
    }

    fn parse_event(&mut self, bytes: Vec<u8>) {
        println!("parsing event");
        if bytes.len() == 0 {
            return;
        }

        let event = Event::from_bytes(bytes);
        let mut conns = self.conns.borrow_mut();
        let id = self.id.clone().unwrap();
        let address = if event.is_error() { &id } else { &self.server_addr };
        let tx = conns.get_mut(address).unwrap();
        tx.unbounded_send(event).unwrap();
    }

    fn act(&mut self, line: Vec<u8>) {
        match self.state {
            State::GetID => {
                self.parse_id(line);
                self.state = State::Parsing;
            },
            State::Parsing => self.parse_event(line),
        }
    }
}

impl Future for Connection {
    type Item = ();
    type Error = Error;

    fn poll(&mut self) -> Poll<self::Item, self::Error> {
        loop {
            try_ready!(self.reader.read_buf(&mut self.buf));

            if self.buf.len() == 0 {
                //return Err("broken pipe");
            }

            let buf = self.buf.clone();
            let mut lines = buf.as_slice().split(|x| *x == b'\n').clone();
            
            if let Some(buf) = lines.next_back() {
                self.buf = buf.to_vec();
            }
            
            for l in lines {
                self.act(l.to_vec());
            }
        }

        Ok(Async::NotReady)
    }
}

