# Image classifier app

Aim of this project is to share skills and experience of creating an Android
project. This includes: Android coding experience, CI pipelines, tests and other
decisions made.

Aim: create image classifier app (aka "Hotdog or not" from Silicon Valley
series) based on pre-trained ML-model.

A log track for this project is at
[my blog http://shamil.gumirov.com](http://shamil.gumirov.com).

# Repo workflow, CI and Jira

`master` branch is protected.

Merge to `master` cannot be done if the CI pipeline is red. Tests are executed
in the CI pipeline.

## Jenkins integration

CI setup: any MR or branch push leads to 
Jenkins [jenkins.gotalkmobile.com](https://jenkins.gotalkmobile.com) automated 
build. Build results are published into Artifactory
[repo.gotalkmobile.com](https://repo.gotalkmobile.com). Tests are executed
automatically.

## Jira integration

Jira integration is enabled with "smart commits" (ticket id reference in commit
msg).
