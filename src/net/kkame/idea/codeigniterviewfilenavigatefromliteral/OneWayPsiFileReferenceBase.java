package net.kkame.idea.codeigniterviewfilenavigatefromliteral;

import com.google.common.collect.ComparisonChain;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public abstract class OneWayPsiFileReferenceBase<T extends PsiElement> extends PsiPolyVariantReferenceBase<T> {

    public OneWayPsiFileReferenceBase(T psiElement) {
        super(psiElement, true);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        String fullFileName = computeStringValue();
        Project project = getElement().getProject();
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        final String finalFullFileName = fullFileName;
        final Set<Pair<Integer, VirtualFile>> sortedResults = new TreeSet<Pair<Integer, VirtualFile>>(new Comparator<Pair<Integer, VirtualFile>>() {
            @Override
            public int compare(Pair<Integer, VirtualFile> o1, Pair<Integer, VirtualFile> o2) {
                return ComparisonChain.start().
                        compare(o1.getFirst(), o2.getFirst()).
                        compare(o1.getSecond(), o2.getSecond(), new Comparator<VirtualFile>() {
                            @Override
                            public int compare(VirtualFile o1, VirtualFile o2) {
                                String o1CanonicalPath = o1.getCanonicalPath();
                                String o2CanonicalPath = o2.getCanonicalPath();
                                if (o1CanonicalPath != null && o2CanonicalPath != null) {
                                    return o1CanonicalPath.compareTo(o2CanonicalPath);
                                } else {
                                    return 0;
                                }
                            }
                        }).
                        compare(o1.getSecond().getName(), o2.getSecond().getName()).
                        result();
            }
        });
        fileIndex.iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileOrDir) {
                if (!fileOrDir.isDirectory()) {

                    if(fileOrDir.getPath().contains(finalFullFileName)){
                        sortedResults.add(new Pair<Integer, VirtualFile>(1, fileOrDir));

                    }

                    /*else if (fileOrDir.getName().equalsIgnoreCase(finalCleanFileName)) {
                        sortedResults.add(new Pair<Integer, VirtualFile>(10, fileOrDir));
                    } else if (fileOrDir.getNameWithoutExtension().equalsIgnoreCase(finalCleanFileName)
                            || fileOrDir.getNameWithoutExtension().equalsIgnoreCase(finalCleanFileNameWithoutExtension)) {
                        if (fileOrDir.getFileType().equals(getElement().getContainingFile().getFileType())) {
                            sortedResults.add(new Pair<Integer, VirtualFile>(20, fileOrDir));
                        } else {
                            sortedResults.add(new Pair<Integer, VirtualFile>(30, fileOrDir));
                        }
                    }*/
                }
                return true;
            }
        });
        PsiManager psiManager = PsiManager.getInstance(project);
        ResolveResult[] result = new ResolveResult[sortedResults.size()];
        int i = 0;
        for (Pair<Integer, VirtualFile> pair : sortedResults) {
            PsiFile psiFile = psiManager.findFile(pair.getSecond());
            if (psiFile != null) {
                result[i++] = new PsiElementResolveResult(psiFile);
            }
        }
        return result;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return false;
    }

    @NotNull
    protected abstract String computeStringValue();

}
