## 👀 see also [maid-hub](https://github.com/bobbystrange/maid-hub) and [maid-webui](https://github.com/bobbystrange/maid-webui)
**Note that this project is pending because I'm tired of UI design**

---
## 🤣🤔 A restful-style implement for file system based on cassandra key columns
- #### 🖤️🖕 Done

```sh
### auth via JWT
register, register-confirm, login, passwd, passwd-confirm

### user
useradd, usermod, userdel, whoami

### file
file, ls, tree, flat-tree
mkdir, rename, mv, cp, rm
download, upload, share
share-file, share-ls, share-download
```
## 😡🤡 API invocation process
- /auth/code/image fetch image code for register
- /auth/register register and check the email
- /auth/register/confirm use redirct url including in the email to confirm your register
- /auth/login login with username & password, obtain token

## Notes
1. **root dir id is 0**
2. list-like API limit is 1024 typically
3. tree-like API limit is 65536 typically
4. path level-like API limit is 128 typically

