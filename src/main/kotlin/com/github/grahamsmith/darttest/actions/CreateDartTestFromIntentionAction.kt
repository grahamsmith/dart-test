package com.github.grahamsmith.darttest.actions

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
import com.intellij.psi.*
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.jetbrains.lang.dart.DartFileType
import com.jetbrains.lang.dart.psi.DartClassDefinition

import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

class CreateDartTestFromIntentionAction : IntentionAction {
    override fun startInWriteAction() = true

    override fun getText() = "Create Dart test file"

    override fun getFamilyName() = "Create Dart test"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return file?.fileType?.defaultExtension.equals(DartFileType.INSTANCE.defaultExtension)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {

        if(editor == null || file == null) {
            return
        }

        val fileName = getFilenameSuggestion(editor, project, file.virtualFile).camelToSnakeCase().let {
            "${it}$TEST_FILE_NAME_SUFFIX"
        }

        val projectPath = ProjectFileIndex.SERVICE.getInstance(project).getContentRootForFile(file.virtualFile)
                ?: throw IllegalArgumentException("Unable to get project path")

        val relativePath = VfsUtilCore.getRelativePath(file.virtualFile.parent, projectPath).orEmpty().replace("lib/", "")

        val newPath = when(isAlreadyInTestDirectory(relativePath)) {
            true -> "${projectPath.toNioPath()}$relativePath"
            false -> "${projectPath.toNioPath()}/${CreateDartTestAction.UNIT_TEST_PATH}$relativePath"
        }

        val newDir = VfsUtil.createDirectories(newPath)
        val newDirForReals = PsiDirectoryFactory.getInstance(project).createDirectory(newDir)

        if(fileExists(newDirForReals, fileName)) {
            val existingFile = newDirForReals.virtualFile.findChild("$fileName$DART_FILE_EXTENSION")
            existingFile?.let { FileEditorManager.getInstance(project).openFile(it, true)}
        } else {
            createTestFile(project, fileName, newDirForReals)
        }
    }

    private fun createTestFile(
            project: Project,
            fileName: String,
            dir: PsiDirectory
    ) {
        runUndoTransparentWriteAction {

            val fileTemplateManager = FileTemplateManager.getInstance(project)
            val template = fileTemplateManager.getInternalTemplate("Dart Test File")
            val element: PsiElement
            try {
                element = FileTemplateUtil.createFromTemplate(template, fileName, fileTemplateManager.defaultProperties, dir)
                val psiFile = element.containingFile
                psiFile.virtualFile?.let { FileEditorManager.getInstance(dir.project).openFile(it, true) }
            } catch (e: IncorrectOperationException) {
                throw e
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun getFilenameSuggestion(editor: Editor?, project: Project, file: VirtualFile): String {
        editor?.caretModel?.currentCaret?.offset?.let { offset ->
            val psiElement = PsiManager.getInstance(project).findFile(file)?.findElementAt(offset) ?: return@let

            val classElement = PsiTreeUtil.getParentOfType(psiElement, DartClassDefinition::class.java) ?: return@let

            return "${classElement.name}"
        }

        throw IllegalStateException("Unable to calculate new test file name")
    }

    private fun fileExists(dir: PsiDirectory, fileName: String): Boolean {
        return dir.files.any { file -> file.name == "$fileName$DART_FILE_EXTENSION" }
    }

    private fun isAlreadyInTestDirectory(relativeFilePath: String): Boolean {
        return relativeFilePath.contains(UNIT_TEST_PATH)
    }

    companion object{
        const val UNIT_TEST_PATH = "test/unit-tests/"
        const val TEST_FILE_NAME_SUFFIX = "_test"
        const val DART_FILE_EXTENSION = ".dart"
    }
}