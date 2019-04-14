package net.kkame.idea.codeigniterviewfilenavigatefromliteral;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class FileReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {

        try {
            Class.forName("com.intellij.psi.PsiLiteral");
            registrar.registerReferenceProvider(StandardPatterns.instanceOf(PsiLiteral.class), new PsiReferenceProvider() {
                @NotNull
                public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    return new PsiReference[]{new OneWayPsiFileFromPsiLiteralReference((PsiLiteral) element)};
                }
            });
        } catch (ClassNotFoundException e) {
            //Ok, then. Some JetBrains platform IDE that has no Java support.
        }


        //Always plugin, even in PhpStorm
        registerPluginDependentOneWayReference(registrar, "com.jetbrains.php", "com.jetbrains.php.lang.psi.elements.StringLiteralExpression");

    }


    private void registerPluginDependentOneWayReference(PsiReferenceRegistrar registrar, String pluginId, String className) {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(pluginId));
        ClassLoader classLoader = plugin != null ? plugin.getPluginClassLoader() : getClass().getClassLoader();
        registerGenericOneWayReference(registrar, className, classLoader);
    }

    @SuppressWarnings("unchecked")
    private void registerGenericOneWayReference(PsiReferenceRegistrar registrar, String className, ClassLoader classLoader) {
        try {
            Class<PsiElement> clazz = (Class<PsiElement>) Class.forName(className, true, classLoader);
            registrar.registerReferenceProvider(StandardPatterns.instanceOf(clazz), new PsiReferenceProvider() {
                @NotNull
                public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    return new PsiReference[]{new OneWayPsiFileReference(element)};
                }
            });

        } catch (ClassNotFoundException e) {
            //ignored
        }
    }

}
