
use types::{Group, User};
use server::Id;

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(tag= "type")]
pub enum Event {
    Update(Update),
    Get(Get),
    Notify {id: Id},
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

