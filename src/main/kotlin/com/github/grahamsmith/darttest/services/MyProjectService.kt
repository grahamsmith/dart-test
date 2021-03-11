package com.github.grahamsmith.darttest.services

import com.github.grahamsmith.darttest.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
