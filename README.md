# Image classifier app

Aim of this project is to share skills and experience of creating an Android
project. This includes: Android coding experience, CI pipelines, tests and other
decisions made.

Aim: create image classifier app (aka "Hotdog or not" from Silicon Valley
series) based on pre-trained ML-model.

A log track for this project is at
[my blog http://shamil.gumirov.com](http://shamil.gumirov.com).

# Summary of what's done

* Android app written in kotlin with MVVM and JetPack, TensorFlow Lite image classifier 
model based on CameraX preview frame stream.
* Two tests: unit (Robolectric JVM-based) and instrumented (needs Android emulator to run).
* Repository: Gitlab (github as backup, see below) with safe workflow: development is done 
in feature branch, merge requests to master with Build/Test CI pipeline checks before able 
to merge to the master branch.
* CI: Gitlab is integrated with Jenkins (Github with Bitrise). Build is executed for 
any push to feature branch and after merge to master.
* Project tracking: Jira with Kanban. All commit messages are automapped to Jira tickets 
using "smart commit messages". Merge of MR leads to automatic Jira ticket state change 
(move to Done)
* CD: publishing is done to Artifactory (artefacts repository)
* Set up separate Jenkins slave to run project tests which require Android emulator for 
instrumented testing.

# Repo workflow, CI and Jira

Repositories (same git repo):

[Main at Gitlab](https://gitlab.gotalkmobile.com/shamilg1/image-classifier)

[Backup at Github](https://github.com/sgumirov/image-classifier)

`master` branch is protected.

Merge to `master` cannot be done if the CI pipeline is red. Tests are executed
in the CI pipeline.

## Jenkins integration

CI setup: any MR or branch push leads to 
Jenkins [jenkins.gotalkmobile.com](https://jenkins.gotalkmobile.com) automated 
build. Build results are published into Artifactory
[repo.gotalkmobile.com](https://repo.gotalkmobile.com). Tests are executed
automatically.

## Bitrise integration

As an alternative a Bitrise/Github integration has been set up (as public project):

[Bitrise build](https://app.bitrise.io/build/9aa207dae34f3055)

## Jira integration

Jira/Gitlab integration is enabled with "smart commits" (ticket id reference in 
commit msg).
[Jira (needs login)](https://jira.gotalkmobile.com)

## Google Play

Google play publish process is ongoing (Internal testing phase).

# License

[GNU GPL 3.0](https://www.gnu.org/licenses/gpl-3.0.html).

# Fonts used

[TH Sarabun New](https://fontlibrary.org/en/font/thsarabun-new) licensed under 
GNU GPL 3.0.
