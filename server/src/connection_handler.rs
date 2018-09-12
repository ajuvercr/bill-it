
use std::net::SocketAddr;
use std::collections::HashMap;
use std::cell::RefCell;
use std::rc::Rc;
use std::io::{Error, ErrorKind};
use std::mem;


use futures::sync::mpsc;
use futures::{Future, Poll, Stream};

use tokio_core::net::{TcpStream};

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

    pub fn new_connection(&self, stream: TcpStream, _addr: SocketAddr) {
        use tokio_io::AsyncRead;

        let (reader, writer) = stream.split();

        let (tx, rx) = mpsc::unbounded::<Event>();

        let conn = Connection::new(tx, reader, self.conns.clone(), self.server_addr.clone())
            .then(|e| {
                println!("{:?}", e);
                e
            });

        let socket_writer = rx.fold(writer, |writer, event| {
            let amt = io::write_all(writer, event.into_bytes());
            let amt = amt.map(|(writer, _)| writer);
            amt.map_err(|_| ())
        });

        let socket_reader = conn;
        let connection = socket_reader.select(socket_writer.then(|_| Ok(())));

        self.handle.spawn(connection.then(|_| Ok(())));
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

    fn parse_id(&mut self, id_bytes: Vec<u8>) -> bool {
        let id = String::from_utf8(id_bytes).unwrap();
        let sender = mem::replace(&mut self.id, Ok(id.clone()));
        println!("new connection with id {}", id);
        let mut conns = self.conns.borrow_mut();

        if conns.contains_key(&id) {
            // when sender goes out of scope, Unbounded sender get's closed
            // so Unbounded Reciever get's closed, so future stops
            let sender = sender.unwrap_err();
            sender.unbounded_send(Event::invalid_connect()).unwrap();
            println!("Invalid connect, bumping");
            false
        } else {
            conns.insert(id, sender.unwrap_err());
            println!("connection map: {:?}", conns.keys());
            true
        }
    }

    fn parse_event(&mut self, bytes: Vec<u8>) -> bool {
        println!("parsing event for {:?}", self.id);

        if bytes.len() == 0 {
            return false;
        }

        let event = Event::from_bytes(bytes);
        let mut conns = self.conns.borrow_mut();
        let id = self.id.clone().unwrap();
        let address = if event.is_error() { &id } else { &self.server_addr };
        let tx = conns.get_mut(address).unwrap();
        tx.unbounded_send(event).unwrap();
        true
    }

    fn act(&mut self, line: Vec<u8>) -> bool {
        match self.state {
            State::GetID => {
                self.state = State::Parsing;
                self.parse_id(line)
            },
            State::Parsing => self.parse_event(line),
        }
    }

    fn disconnect(&self) {
        if let Ok(addr) = self.id.clone() {
            self.conns.borrow_mut().remove(&addr);
            println!("disconnecting {:?}", addr);
        }
    }
}

impl Future for Connection {
    type Item = ();
    type Error = Error;

    fn poll(&mut self) -> Poll<(), self::Error> {
        loop {
            try_ready!(self.reader.read_buf(&mut self.buf));

            if self.buf.len() == 0 {
                self.disconnect();
                return Err(Error::new(ErrorKind::BrokenPipe, "broken pipe"));
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
    }
}

