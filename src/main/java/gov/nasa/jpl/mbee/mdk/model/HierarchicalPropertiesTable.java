package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class HierarchicalPropertiesTable extends Table {
    protected int floatingPrecision;
    protected int maxDepth;
    protected List<String> topIncludeTypeName;
    protected List<String> topExcludeTypeName;
    protected List<Stereotype> topIncludeStereotype;
    protected List<Stereotype> topExcludeStereotype;
    protected List<String> topIncludeName;
    protected List<String> topExcludeName;
    protected int topAssociationType;
    protected List<String> topOrder;
    protected boolean showType;
    protected boolean includeInherited;

    public boolean isIncludeInherited() {
        return includeInherited;
    }

    public void setIncludeInherited(boolean includeInherited) {
        this.includeInherited = includeInherited;
    }

    public void setFloatingPrecision(int floatingPrecision) {
        this.floatingPrecision = floatingPrecision;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setTopIncludeTypeName(List<String> topIncludeTypeName) {
        this.topIncludeTypeName = topIncludeTypeName;
    }

    public void setTopExcludeTypeName(List<String> topExcludeTypeName) {
        this.topExcludeTypeName = topExcludeTypeName;
    }

    public void setTopIncludeStereotype(List<Stereotype> topIncludeStereotype) {
        this.topIncludeStereotype = topIncludeStereotype;
    }

    public void setTopExcludeStereotype(List<Stereotype> topExcludeStereotype) {
        this.topExcludeStereotype = topExcludeStereotype;
    }

    public void setTopIncludeName(List<String> topIncludeName) {
        this.topIncludeName = topIncludeName;
    }

    public void setTopExcludeName(List<String> topExcludeName) {
        this.topExcludeName = topExcludeName;
    }

    public void setTopAssociationType(int topAssociationType) {
        this.topAssociationType = topAssociationType;
    }

    public void setTopOrder(List<String> topOrder) {
        this.topOrder = topOrder;
    }

    public void setShowType(boolean showType) {
        this.showType = showType;
    }

    public int getFloatingPrecision() {
        return floatingPrecision;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public List<String> getTopIncludeTypeName() {
        return topIncludeTypeName;
    }

    public List<String> getTopExcludeTypeName() {
        return topExcludeTypeName;
    }

    public List<Stereotype> getTopIncludeStereotype() {
        return topIncludeStereotype;
    }

    public List<Stereotype> getTopExcludeStereotype() {
        return topExcludeStereotype;
    }

    public List<String> getTopIncludeName() {
        return topIncludeName;
    }

    public List<String> getTopExcludeName() {
        return topExcludeName;
    }

    public int getTopAssociationType() {
        return topAssociationType;
    }

    public List<String> getTopOrder() {
        return topOrder;
    }

    public boolean isShowType() {
        return showType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();
        Integer maxDepth = (Integer) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.hierarchicalPropertiesTableStereotype, "maxDepth", 0);
        List<String> topIncludeTypeName = DocGenUtils
                .getElementNames((Collection<NamedElement>) GeneratorUtils.getListProperty(dgElement,
                        DocGenProfile.hierarchicalPropertiesTableStereotype, "topIncludeTypeName",
                        new ArrayList<Property>()));
        List<String> topExcludeTypeName = DocGenUtils
                .getElementNames((Collection<NamedElement>) GeneratorUtils.getListProperty(dgElement,
                        DocGenProfile.hierarchicalPropertiesTableStereotype, "topExcludeTypeName",
                        new ArrayList<Property>()));
        List<Stereotype> topIncludeStereotype = (List<Stereotype>) GeneratorUtils.getListProperty(dgElement,
                DocGenProfile.hierarchicalPropertiesTableStereotype, "topIncludeStereotype",
                new ArrayList<Stereotype>());
        List<Stereotype> topExcludeStereotype = (List<Stereotype>) GeneratorUtils.getListProperty(dgElement,
                DocGenProfile.hierarchicalPropertiesTableStereotype, "topExcludeStereotype",
                new ArrayList<Stereotype>());
        List<String> topIncludeName = DocGenUtils.getElementNames((Collection<NamedElement>) GeneratorUtils
                .getListProperty(dgElement, DocGenProfile.hierarchicalPropertiesTableStereotype,
                        "topIncludeName", new ArrayList<Property>()));
        List<String> topExcludeName = DocGenUtils.getElementNames((Collection<NamedElement>) GeneratorUtils
                .getListProperty(dgElement, DocGenProfile.hierarchicalPropertiesTableStereotype,
                        "topExcludeName", new ArrayList<Property>()));
        Integer topAssociationType = (Integer) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.hierarchicalPropertiesTableStereotype, "topAssociationType", 0);
        List<String> topOrder = DocGenUtils.getElementNames((Collection<NamedElement>) GeneratorUtils
                .getListProperty(dgElement, DocGenProfile.hierarchicalPropertiesTableStereotype, "topOrder",
                        new ArrayList<Property>()));
        if (!topIncludeName.isEmpty() && topOrder.isEmpty()) {
            topOrder = topIncludeName;
        }

        setFloatingPrecision((Integer) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.precisionChoosable, "floatingPrecision", -1));
        setMaxDepth(maxDepth);
        setTopIncludeTypeName(topIncludeTypeName);
        setTopExcludeTypeName(topExcludeTypeName);
        setTopIncludeStereotype(topIncludeStereotype);
        setTopExcludeStereotype(topExcludeStereotype);
        setTopIncludeName(topIncludeName);
        setTopExcludeName(topExcludeName);
        setTopAssociationType(topAssociationType);
        setTopOrder(topOrder);
        setIncludeInherited((Boolean) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.inheritedChoosable, "includeInherited", false));
    }
}
