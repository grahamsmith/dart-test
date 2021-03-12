package com.github.grahamsmith.darttest.actions

import com.github.grahamsmith.darttest.MyBundle
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.ModuleTypeWithWebFeatures
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiFile
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.impl.file.PsiDirectoryFactory
import icons.DartIcons
import java.lang.IllegalArgumentException
import com.intellij.util.IncorrectOperationException

import com.intellij.psi.PsiElement

import org.bouncycastle.asn1.iana.IANAObjectIdentifiers.directory

import com.intellij.openapi.fileEditor.FileEditorManager

import com.intellij.ide.fileTemplates.FileTemplateManager

import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.psi.search.FileTypeIndex
import com.jetbrains.lang.dart.DartFileType
import com.jetbrains.lang.dart.sdk.DartSdk
import org.bouncycastle.asn1.iana.IANAObjectIdentifiers
import java.lang.Character.getName
import java.lang.Exception


class CreateDartTestAction : CreateFileFromTemplateAction(MyBundle.messagePointer("action.title.dart-test.file"),
        MyBundle.messagePointer("action.description.create.dart-test.file"),
        DartIcons.Dart_test) {

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
                .setTitle(MyBundle.message("new.dart-test.file.title"))
                .addKind(MyBundle.message("list.item.dart-test.file"), DartIcons.Dart_test, "Dart Test File")

    }

    override fun isAvailable(dataContext: DataContext?): Boolean {

        val context = dataContext ?: return false

        val module = LangDataKeys.MODULE.getData(context)
        return super.isAvailable(dataContext) && module != null &&
                (FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, module.moduleContentScope) ||
                        DartSdk.getDartSdk(module.project) != null && ModuleTypeWithWebFeatures.isAvailable(module))
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return MyBundle.message("title.create.dart-test.file.0", newName)
    }

    override fun createFileFromTemplate(name: String?, template: FileTemplate?, dir: PsiDirectory?): PsiFile {

        if(dir == null) {
            throw IllegalArgumentException("Provided directory is null")
        }

        val className = FileUtilRt.getNameWithoutExtension(name!!)
        val project = dir.project

        val projectPath = ProjectFileIndex.SERVICE.getInstance(project).getContentRootForFile(dir.virtualFile)
                ?: throw IllegalArgumentException("Unable to get project path")

        val relativePath = VfsUtilCore.getRelativePath(dir.virtualFile, projectPath).orEmpty().replace("lib/", "")

        val newPath = when(isAlreadyInTestDirectory(relativePath)) {
            true -> "${projectPath.toNioPath()}/$relativePath"
            false -> "${projectPath.toNioPath()}/$UNIT_TEST_PATH$relativePath"
        }

        val newDir = VfsUtil.createDirectories(newPath)
        val newDirForReals = PsiDirectoryFactory.getInstance(project).createDirectory(newDir)

        return super.createFileFromTemplate("${className}_test.dart", template, newDirForReals)
    }

    private fun isAlreadyInTestDirectory(relativeFilePath: String): Boolean {
        return relativeFilePath.contains(UNIT_TEST_PATH)
    }

    companion object{
        const val UNIT_TEST_PATH = "test/unit-tests/"
    }
}