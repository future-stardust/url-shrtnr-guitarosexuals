# GuitaroSexuals🎸🕺️ - URL shortener 

## Students group

- Серіков Олег [nameless.as.oleg@gmail.com](mailto:nameless.as.oleg@gmail.com)
- Пузир Дмитрій [slymahel@gmail.com](mailto:slymahel@gmail.com)
- Губенко Максим [mhubstudy@gmail.com](mailto:mhubstudy@gmail.com)
- Кривонос Андрій [drew.krvns@gmail.com](mailto:drew.krvns@gmail.com)
- Бортнік Василь [bortnsk00@gmail.com](mailto:bortnsk00@gmail.com)

## Design document

[Click to view](https://docs.google.com/document/d/1K0a43_MgFRw3c40RHeyPpuDqQIkwXy3h1ZxDGZjQp1o/edit?usp=sharing)

### System structure

After the third laboratory assignment groups will switch projects with one another. Because of this,
all projects have to have the same high-level structure. Also, this is the reason why we will not
modify project dependencies.

There are four modules:
- `httphandler` - **HTTP Frontend** - Handles HTTP requests, provides REST API
- `users` - **Users** - Authenticates and manages users
- `urls` - **URLs** - Operates URLs (creation, usage, removal)
- `database` - **Database** - Handles all DB operations and file system interaction

## Requirements

### Host Server
Operating System: UNIX-like

### Java
This is a Java project, so you will need an environment with installed [JDK] 15. For installation, 
you could use:
- [sdkman] on Linux/MacOS 
- [AdoptOpenJDK] on Windows

### Checkstyle
We use [checkstyle] to ensure coding standards. We use Google rules (local copy `./config/checkstyle/checkstyle.xml`).

## How to start development

1. Clone this repo
2. Open the project directory in your editor/IDE
3. Configure code style settings
  
[JDK]: https://en.wikipedia.org/wiki/Java_Development_Kit
[sdkman]: https://sdkman.io/
[AdoptOpenJDK]: https://adoptopenjdk.net/
[7 rules of good commit messages]: https://chris.beams.io/posts/git-commit/#seven-rules
[checkstyle]: https://checkstyle.org/


## Handbook📚
Follow the guidance provided in this [link](/HANDBOOK.md).