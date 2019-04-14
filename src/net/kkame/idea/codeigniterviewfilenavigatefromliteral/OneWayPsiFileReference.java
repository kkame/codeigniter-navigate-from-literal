package net.kkame.idea.codeigniterviewfilenavigatefromliteral;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class OneWayPsiFileReference extends OneWayPsiFileReferenceBase<PsiElement> {

    public OneWayPsiFileReference(PsiElement psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    protected String computeStringValue() {
        String text = getElement().getText();
        if (text.length() >= 2 && (text.startsWith("\"") && text.endsWith("\"") || text.startsWith("'") && text.endsWith("'"))) {
            return text.substring(1, text.length() - 1);
        } else {
            //Some strange literal, has no quotes. Try anyway
            return text;
        }
    }

}
