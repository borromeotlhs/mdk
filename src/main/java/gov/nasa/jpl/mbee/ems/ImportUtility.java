package gov.nasa.jpl.mbee.ems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import gov.nasa.jpl.graphs.DirectedEdgeVector;
import gov.nasa.jpl.graphs.DirectedGraphHashSet;
import gov.nasa.jpl.graphs.algorithms.TopologicalSort;
import gov.nasa.jpl.mbee.ems.validation.PropertyValueType;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

public class ImportUtility {
    public static final Set<String> VALUESPECS = new HashSet<String>(Arrays.asList(
            new String[] {"LiteralInteger", "LiteralString", "LiteralBoolean", 
                    "LiteralUnlimitedNatural", "Expression", "InstanceValue", 
                    "ElementValue", "OpaqueExpression", "Interval", "LiteralReal", 
                    "Duration", "DurationInterval", "TimeInterval", "TimeExpression", "StringExpression"}
                    
       ));
    
    public static List<JSONObject> getCreationOrder(List<JSONObject> newElements) {
        DirectedGraphHashSet<JSONObject, DirectedEdgeVector<JSONObject>> graph = new DirectedGraphHashSet<JSONObject, DirectedEdgeVector<JSONObject>>();
        Map<String, JSONObject> id2ob = new HashMap<String, JSONObject>();
        for (JSONObject ob: newElements) {
            String sysmlid = (String)ob.get("sysmlid");
            if (sysmlid == null)
                continue;
            id2ob.put(sysmlid, ob);
            graph.addVertex(ob);
        }
        for (JSONObject ob: newElements) {
            String sysmlid = (String)ob.get("sysmlid");
            String ownerid = (String)ob.get("owner");
            Element newE = ExportUtility.getElementFromID(sysmlid);
            Element ownerE = ExportUtility.getElementFromID(ownerid);
            if (ownerE == null && !id2ob.containsKey(ownerid))
                return null; //cannot all be created
            if (newE != null || ownerE != null)
                continue;
            JSONObject newj = id2ob.get(sysmlid);
            JSONObject ownerj = id2ob.get(ownerid);
            graph.addEdge(newj, ownerj);
        }
        SortedSet<JSONObject> reverse = (new TopologicalSort()).topological_sort(graph);
        List<JSONObject> toposort = new ArrayList<JSONObject>(reverse);
        Collections.reverse(toposort);
        return toposort;
    }
    
    public static Element createElement(JSONObject ob) {
        Project project = Application.getInstance().getProject();
        ElementsFactory ef = project.getElementsFactory();
        project.getCounter().setCanResetIDForObject(true);
        String ownerId = (String) ob.get("owner");
        if (ownerId == null || ownerId.isEmpty())
            return null;
        if (ExportUtility.getElementFromID(ownerId) == null)
            return null;
        // For all new elements the should be the following fields
        // should be present: name, owner, and documentation
        //
        String sysmlID = (String) ob.get("sysmlid");
        Element existing = ExportUtility.getElementFromID(sysmlID);
        if (existing != null)
            return null; //maybe jms feedback
        JSONObject specialization = (JSONObject) ob.get("specialization");
        String elementType = "Element";
        if (specialization != null) {
            elementType = (String) specialization.get("type");
        }
        Element newE = null;
        if (elementType.equalsIgnoreCase("view")) {
            Class view = ef.createClassInstance();
            Stereotype sysmlView = Utils.getViewClassStereotype();
            StereotypesHelper.addStereotype(view, sysmlView);
            newE = view;
        } else if (elementType.equalsIgnoreCase("Property")) {
            JSONArray vals = (JSONArray) specialization.get("value");

            // Check if this is a slot. If so, process
            // the associated values; otherwise continue
            // to process the Property element using only the
            // first value in the array.
            //
            Boolean isSlot = (Boolean) specialization.get("isSlot");
            if ((isSlot != null) && (isSlot == true)) {
                Slot newSlot = ef.createSlotInstance();
                setSlotValues(newSlot, vals);
                newE = newSlot;
            }
            else {
                Property newProperty = ef.createPropertyInstance();
                setPropertyDefaultValue(newProperty, vals);
                newE = newProperty;
            }
        } else if (elementType.equalsIgnoreCase("Dependency")
                || elementType.equalsIgnoreCase("Expose")
                || elementType.equalsIgnoreCase("DirectedRelationship")) {
            Dependency newDependency = ef.createDependencyInstance();
            setRelationshipEnds(newDependency, specialization);
            newE = newDependency;
        } else if (elementType.equalsIgnoreCase("Generalization") || elementType.equalsIgnoreCase("Conform")) {
            Generalization newGeneralization = ef.createGeneralizationInstance();
            setRelationshipEnds(newGeneralization, specialization);
            if (elementType.equalsIgnoreCase("Conform")) {
                Stereotype conform = Utils.getSysML14ConformsStereotype();
                StereotypesHelper.addStereotype(newGeneralization, conform);
            }
            newE = newGeneralization;
        } else if (elementType.equalsIgnoreCase("Package")) {
            Package newPackage = ef.createPackageInstance();
            newE = newPackage;
        } else if (elementType.equalsIgnoreCase("Constraint")) {
            Constraint c = ef.createConstraintInstance();
            setConstraintSpecification(c, specialization);
            newE = c;
        } else if (elementType.equalsIgnoreCase("Product")) {
            Class prod = ef.createClassInstance();
            Stereotype product = Utils.getProductStereotype();
            StereotypesHelper.addStereotype(prod, product);
            newE = prod;
        } else {
            Class newElement = ef.createClassInstance();
            newE = newElement;
        }
        setName(newE, ob);
        setOwner(newE, ob);
        setDocumentation(newE, ob);
        newE.setID(sysmlID);
        return newE;
    }
    
    public static void updateElement(Element e, JSONObject o) {
        setName(e, o);
        setDocumentation(e, o);
        JSONObject spec = (JSONObject)o.get("specialization");
        if (spec != null) {
            String type = (String)spec.get("type");
            if (type != null && type.equals("Property") && e instanceof Property)
                setPropertyDefaultValue((Property)e, (JSONArray)spec.get("value"));
            if (type != null && type.equals("Property") && e instanceof Slot)
                setSlotValues((Slot)e, (JSONArray)spec.get("value"));
            if (type != null && e instanceof DirectedRelationship)
                setRelationshipEnds((DirectedRelationship)e, spec);
            if (type != null && e instanceof Constraint && type.equals("Constraint"))
                setConstraintSpecification((Constraint)e, spec);
        }
    }
    
    public static void setName(Element e, JSONObject o) {
        String name = (String)o.get("name");
        setName(e, name);
    }
    
    public static void setName(Element e, String name) {
        if (e instanceof NamedElement && name != null)
            ((NamedElement)e).setName(ExportUtility.unescapeHtml(name));
    }
    
    public static void setOwner(Element e, JSONObject o) {
        String ownerId = (String) o.get("owner");
        if ((ownerId == null) || (ownerId.isEmpty())) {
            Application.getInstance().getGUILog().log("Owner not specified for mms sync add");
            return;
        }
        Element owner = ExportUtility.getElementFromID(ownerId);
        if (owner == null) {
            Application.getInstance().getGUILog().log("Owner not found for mms sync add");
            return;
        }
        e.setOwner(owner);
    }
    
    public static void setDocumentation(Element e, JSONObject o) {
        String doc = (String)o.get("documentation");
        setDocumentation(e, doc);
    }
    
    public static void setDocumentation(Element e, String doc) {
        if (doc != null)
            ModelHelper.setComment(e, Utils.addHtmlWrapper(doc));
    }
    
    public static void setRelationshipEnds(DirectedRelationship dr, JSONObject specialization) {
        String sourceId = (String) specialization.get("source");
        String targetId = (String) specialization.get("target");
        Element source = ExportUtility.getElementFromID(sourceId);
        Element target = ExportUtility.getElementFromID(targetId);
        if (source != null && target != null) {
            ModelHelper.setSupplierElement(dr, target);
            ModelHelper.setClientElement(dr, source);
        }
    }
    
    public static void setPropertyDefaultValue(Property p, JSONArray values) {
        if (values != null && values.size() > 0)
            p.setDefaultValue(createValueSpec((JSONObject)values.get(0)));
        if (values != null && values.isEmpty())
            p.setDefaultValue(null);
    }
    
    public static void setSlotValues(Slot s, JSONArray values) {
        if (values == null)
            return;
        s.getValue().clear();
        for (Object o: values) {
            s.getValue().add(createValueSpec((JSONObject)o));
        }
    }
    
    public static void setConstraintSpecification(Constraint c, JSONObject spec) {
        JSONObject sp = (JSONObject)spec.get("constraintSpecification");
        if (sp != null) {
            c.setSpecification(createValueSpec(sp));
        }
    }
    
    public static ValueSpecification createValueSpec(JSONObject o) {
        ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
        String valueType = (String)o.get("type");
        ValueSpecification newval = null;
        PropertyValueType propValueType = PropertyValueType.valueOf(valueType);
        
        switch ( propValueType ) {
        case LiteralString:
            newval = ef.createLiteralStringInstance();
            ((LiteralString)newval).setValue(Utils.addHtmlWrapper((String)o.get("string")));
            break;
        case LiteralInteger:
            newval = ef.createLiteralIntegerInstance();
            ((LiteralInteger)newval).setValue(((Long)o.get("integer")).intValue());
            break;
        case LiteralBoolean:
            newval = ef.createLiteralBooleanInstance();
            ((LiteralBoolean)newval).setValue((Boolean)o.get("boolean"));
            break;
        case LiteralUnlimitedNatural:
            newval = ef.createLiteralUnlimitedNaturalInstance();
            ((LiteralUnlimitedNatural)newval).setValue(((Long)o.get("naturalValue")).intValue());
            break;
        case LiteralReal:
            Double value;
            if (o.get("double") instanceof Long)
                value = Double.parseDouble(((Long)o.get("double")).toString());
            else
                value = (Double)o.get("double");

            newval = ef.createLiteralRealInstance();
            ((LiteralReal)newval).setValue(value);
            break;
        case ElementValue:
            Element find = ExportUtility.getElementFromID((String)o.get("element"));
            if (find == null) {
                Application.getInstance().getGUILog().log("Element with id " + o.get("element") + " not found!");
                break;
            }
            newval = ef.createElementValueInstance();
            ((ElementValue)newval).setElement(find);
            break;
        case InstanceValue:
            Element findInst = ExportUtility.getElementFromID((String)o.get("instance"));
            if (findInst == null) {
                Application.getInstance().getGUILog().log("Element with id " + o.get("instance") + " not found!");
                break;
            }
            if (!(findInst instanceof InstanceSpecification)) {
                Application.getInstance().getGUILog().log("Element with id " + o.get("instance") + " is not an instance spec, cannot be put into an InstanceValue.");
                break;
            }
            newval = ef.createInstanceValueInstance();
            ((InstanceValue)newval).setInstance((InstanceSpecification)findInst);
            break;
        default:
            Debug.error("Bad PropertyValueType: " + valueType);
        };
        return newval;
    }
}