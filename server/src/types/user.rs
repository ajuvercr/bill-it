
use super::Id;

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct User {
    name: String,
    email: String,
    pub id: Id,
    pub friends: Vec<Id>,
    pub groups: Vec<Id>,
}

impl User {
    pub fn new() -> User {
        User {
            name: "John".to_string(),
            email: "blabla@gmail.com".to_string(),
            id: "Some random user id".to_string(),
            friends: Vec::new(),
            groups: Vec::new(),
        }
    }
}