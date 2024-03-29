package com.github.grahamsmith.darttest.actions

import com.github.grahamsmith.darttest.actions.ActionHelper.Companion.DART_FILE_EXTENSION
import com.github.grahamsmith.darttest.actions.ActionHelper.Companion.TEST_FILE_NAME_SUFFIX
import com.github.grahamsmith.darttest.actions.ActionHelper.Companion.UNIT_TEST_PATH
import com.github.grahamsmith.darttest.extensions.camelToSnakeCase
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.jetbrains.lang.dart.DartFileType
import com.jetbrains.lang.dart.psi.DartClassDefinition

class CreateDartTestFromIntentionAction : IntentionAction {
    override fun startInWriteAction() = true

    override fun getText() = "Create Dart test file"

    override fun getFamilyName() = "Create Dart test"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return file?.fileType?.defaultExtension.equals(DartFileType.INSTANCE.defaultExtension)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {

        if (editor == null || file == null) {
            return
        }

        val fileName = getFilenameSuggestion(editor, project, file.virtualFile).camelToSnakeCase().let {
            "${it}$TEST_FILE_NAME_SUFFIX"
        }

        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val projectPath = projectFileIndex.getContentRootForFile(file.virtualFile)
            ?: throw IllegalArgumentException("Unable to get project path")

        val relativePath = VfsUtilCore.getRelativePath(file.virtualFile.parent, projectPath)
            .orEmpty()
            .replace("lib/", "")

        val newPath = when (isAlreadyInTestDirectory(relativePath)) {
            true -> "${projectPath.toNioPath()}$relativePath"
            false -> "${projectPath.toNioPath()}/$UNIT_TEST_PATH$relativePath"
        }

        val newDir = VfsUtil.createDirectories(newPath)
        val newDirForReals = PsiDirectoryFactory.getInstance(project).createDirectory(newDir)

        if (fileExists(newDirForReals, fileName)) {
            val existingFile = newDirForReals.virtualFile.findChild("$fileName$DART_FILE_EXTENSION")
            existingFile?.let { FileEditorManager.getInstance(project).openFile(it, true) }
        } else {
            createTestFile(project, fileName, newDirForReals)
        }
    }

    private fun createTestFile(project: Project, fileName: String, dir: PsiDirectory) {
        runUndoTransparentWriteAction {

            val fileTemplateManager = FileTemplateManager.getInstance(project)
            val fileEditorManager = FileEditorManager.getInstance(project)
            val template = fileTemplateManager.getInternalTemplate("Dart Test File")
            val properties = fileTemplateManager.defaultProperties

            try {
                FileTemplateUtil.createFromTemplate(template, fileName, properties, dir).containingFile?.let {
                    it.virtualFile?.let { virtualFile -> fileEditorManager.openFile(virtualFile, true) }
                }
            } catch (e: IncorrectOperationException) {
                println("Error creating the test file")
            } catch (e: Exception) {
                println("Error creating the test file")
            }
        }
    }

    private fun getFilenameSuggestion(editor: Editor?, project: Project, file: VirtualFile): String {
        editor?.caretModel?.currentCaret?.offset?.let { offset ->

            val currentFile = PsiManager.getInstance(project).findFile(file)

            val psiElement = currentFile?.findElementAt(offset)

            val classElement = PsiTreeUtil.getParentOfType(psiElement, DartClassDefinition::class.java)

            return classElement?.name ?: currentFile?.name?.removeSuffix(".dart") ?: return@let
        }

        throw IllegalStateException("Unable to calculate new test file name")
    }

    private fun fileExists(dir: PsiDirectory, fileName: String): Boolean {
        return dir.files.any { file -> file.name == "$fileName$DART_FILE_EXTENSION" }
    }

    private fun isAlreadyInTestDirectory(relativeFilePath: String): Boolean {
        return relativeFilePath.contains(UNIT_TEST_PATH)
    }
}
