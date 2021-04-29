# modules/

This directory will hold the git submodules for this project, because
a decision was made to split the project into separate modules, each
one having its own repository and git history.

These modules are to be developed independently but with a predefined
API, which will allow them to be tied up together by this repository,
hence producing the final system.

---

## Contents

This directory must contain the following subdirectories,
each one being a [git submodule][1].

1. II_MES_Request_Handler
2. II_MES_Operations_Manager
3. II_MES_Data_Manager
4. II_MES_OPC-UA


[1]: https://git-scm.com/book/en/v2/Git-Tools-Submodules
