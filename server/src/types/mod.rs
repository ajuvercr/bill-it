pub use self::user::User;
pub use self::group::Group;

pub mod user;
pub mod group;

pub type Id = String;

use serde_json;

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(tag="type")]
pub enum Type {
    Group(Group),
    User(User),
    //TODO: should be better error
    Error{error: String},
}

impl Type {
    pub fn into_bytes(&self) -> Vec<u8> {
        match serde_json::to_string(self) {
            Ok(mut out) => {
                out.push_str("\n");
                out.into_bytes()
            },
            Err(e) => {
                println!("error type into bytes: {:?}", e);
                "\n".to_string().into_bytes()
            }
        }
    }
}