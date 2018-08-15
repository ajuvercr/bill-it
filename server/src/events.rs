
use types::{Group, User};
use server::Id;

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(tag= "type")]
pub enum Event {
    Update(Update),
    Get(Get),
    Notify {id: Id},
    Error {error: String},
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
    User {id: Id},
    Group {id: Id},
}

impl Event {
    pub fn from_bytes(bytes: Vec<u8>) -> Event {
        Event::Error {error: "You died".to_string()}
    }

    pub fn into_bytes(&self) -> Vec<u8> {
        Vec::new()
    }

    pub fn is_error(&self) -> bool {
        match self {
            Event::Error {error} => true,
            _ => false,
        }
    }
}

