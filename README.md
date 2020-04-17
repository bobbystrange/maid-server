## 👀 see also [maid-hub](https://github.com/bobbystrange/maid-hub) and [maid-webui](https://github.com/bobbystrange/maid-webui)
**Note that this project is pending because I'm tired of UI design**

---
## 🤣🤔 A restful-style implement for file system based on cassandra key columns
- user_file, mapping a root directory
> uid, root user id
> path, file path like /path/to/a/directory/or/file
> items, only dir, a set which contains short name of sub files, like {'etc', 'usr', ...} for /

- file, mapping physical files
> digest, md5 digest for normal not-empty file
> type, media type
> size, file size, unit is byte
>
## 🐱👮 Artificial restriction of FS
> sub item count:   65536
> file chain level: 128

## 🍔👩‍🎤 Restful API via http1.1

- #### 🖤️🖕 Done

```sh
### auth via JWT
register
login
passwd

### user
useradd
usermod
userdel
whoami

### file
>
mkdir
ls
tree
rename
mv
cp -r
rm -rf
# batch of mv, cp and rm
download
upload
```

- #### 🤪🐫️ Plan

```sh
# user
id -g && id -u
users
groups
groupadd
groupmod
groupdel
chown
chmod

# file
>>
open
cat
zip
unzip
# trash
# favor
# history
```

