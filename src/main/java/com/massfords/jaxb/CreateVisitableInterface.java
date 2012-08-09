package com.massfords.jaxb;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JTypeVar;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

import java.util.Set;

public class CreateVisitableInterface extends CodeCreator {

    private JDefinedClass visitor;
    private JDefinedClass transformer;

    public CreateVisitableInterface(JDefinedClass visitor, JDefinedClass transformer, Outline outline, JPackage jPackage) {
        super(outline, jPackage);
        this.visitor = visitor;
        this.transformer = transformer;
    }

    @Override
    protected void run(Set<ClassOutline> classes) {
        setOutput( outline.getClassFactory().createInterface(jpackage, "Visitable", null) );
        
        getOutput().method(JMod.PUBLIC, void.class, "accept").param(visitor, "aVisitor");
        JMethod trans = getOutput().method(JMod.PUBLIC, void.class, "accept");
        JTypeVar genericType = trans.generify("T");
        trans.type(genericType);
        trans.param(transformer.narrow(genericType), "aTransformer");
        
        for(ClassOutline classOutline : classes) {
            classOutline.implClass._implements(getOutput());
        }
    }

}
