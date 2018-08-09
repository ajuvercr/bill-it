
use types::User;

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Group {
    name: String, 
    users: Vec<User>,
}

impl Group {
    pub fn new() -> Group {
        Group {
            name: "Test group ofcourse".to_string(),
            users: Vec::new(),
        }
    }

    pub fn add_user(&mut self, u: User) {
        self.users.push(u);
    }
}