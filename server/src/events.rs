
use futures::sync::mpsc;

use types::{Group, User, Type, Id};
use serde_json;

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(tag= "type")]
pub enum Event {
    Update(Update),
    Get(Get),
    Notify(Type),
    Error {error: String},
}

#[derive(Debug, Clone)]
pub enum ServerEvent {
    Connect(mpsc::UnboundedSender<Type>),
    Event(Event),
    Disconnect,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(untagged)]
pub enum SendTypes {
    User(User),
    Group(Group),
    Error(String)
}

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(untagged)]
pub enum Update {
    User {user: User, id: Id},
    Group {group: Group, id: Id},
}

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(untagged)]
pub enum Get {
    User {user: Id},
    Group {group: Id},
}

impl Event {
    pub fn from_bytes(bytes: Vec<u8>) -> Event {
        match serde_json::from_slice(&bytes) {
            Ok(e) => e,
            Err(e) => Event::Error{error: format!("couldn't derive from bytes: {}", e)},
        }
    }

    pub fn into_bytes(&self) -> Vec<u8> {
        let mut out = serde_json::to_string(self).unwrap();
        out.push_str("\n");
        out.into_bytes()
    }

    pub fn is_error(&self) -> bool {
        match self {
            Event::Error {error: _} => true,
            _ => false,
        }
    }

    pub fn invalid_connect() -> Event {
        Event::Error{error: "Invalid Connect: id already connected".to_string()}
    }
}

