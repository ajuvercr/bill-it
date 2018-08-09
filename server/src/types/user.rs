

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct User {
    name: String,
    age: u32,
}

impl User {
    pub fn new() -> User {
        User {
            name: "John".to_string(),
            age: 18
        }
    }
}