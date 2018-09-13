
use types::User;
use super::Item;
use super::Expense;
use super::Id;

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Group {
    ownerId: Id,
    name: String,
    pub id: Id,
    pub userIds: Vec<Id>,
    items: Vec<Item>,
    expenses: Vec<Expense>,
}

impl Group {
    pub fn new() -> Group {
        Group {
            name: "Test group ofcourse".to_string(),
            ownerId: "someRandomId".to_string(),
            id: "SomeotherrandomID".to_string(),
            userIds: Vec::new(),
            items: Vec::new(),
            expenses: Vec::new(),
        }
    }

    pub fn add_user(&mut self, u: Id) {
        self.userIds.push(u);
    }
}