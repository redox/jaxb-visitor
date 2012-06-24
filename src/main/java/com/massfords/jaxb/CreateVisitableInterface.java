package com.massfords.jaxb;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

import java.util.Set;

public class CreateVisitableInterface extends CodeCreator {

    private JDefinedClass visitor;

    public CreateVisitableInterface(JDefinedClass visitor, Outline outline, JPackage jPackage) {
        super(outline, jPackage);
        this.visitor = visitor;
    }

    @Override
    protected void run(Set<ClassOutline> classes) {
        setOutput( outline.getClassFactory().createInterface(jpackage, "Visitable", null) );
        getOutput().method(JMod.PUBLIC, void.class, "accept").param(visitor, "aVisitor");
        
        for(ClassOutline classOutline : classes) {
            classOutline.implClass._implements(getOutput());
        }
    }

}
