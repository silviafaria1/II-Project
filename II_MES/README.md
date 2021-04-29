# II_MES
This is a meta-repository for the MES system in II.

This repository will merge the different modules of the system together.

Each one of them will be included as a [git submodule][1] in the [modules/](modules) directory.


# Recommended configuration

This is the recommended configuration for the development of this project:

1. IDE: [IntelliJ IDEA][5] ([download Community Edition][6]) with plugins
 ([instructions][10]):
    1. "Markdown"
    1. ".ignore"
1. Java Development Kit (JDK): [OpenJDK 11][2] 
    ([download][3] and [installation instructions][4])
    1. Add installed jdk to IntelliJ IDEA ([instructions][7])
1. Git ([download][11])
    
## IntelliJ IDEA configurations

1. [Import the project from Github][9]
1. [Add build.xml to the Ant tab][8] (if it exists)
1. [Set the project SDK][12]


[comment]: <> (Reference for links used in the document)

[1]: https://git-scm.com/book/en/v2/Git-Tools-Submodules
[2]: https://openjdk.java.net/projects/jdk/11/
[3]: https://adoptopenjdk.net/index.html?variant=openjdk11&jvmVariant=hotspot
[4]: https://adoptopenjdk.net/installation.html?variant=openjdk11&jvmVariant=hotspot#
[5]: https://www.jetbrains.com/idea/
[6]: https://www.jetbrains.com/idea/download/
[7]: https://www.jetbrains.com/help/idea/sdk.html#define-sdk
[8]: https://www.jetbrains.com/help/idea/adding-build-file-to-project.html
[9]: https://www.jetbrains.com/help/idea/manage-projects-hosted-on-github.html#clone-from-GitHub
[10]: https://www.jetbrains.com/help/idea/managing-plugins.html#top
[11]: https://git-scm.com/downloads
[12]: https://www.jetbrains.com/help/idea/sdk.html#change-project-sdkhttps://www.jetbrains.com/help/idea/sdk.html#change-project-sdk
