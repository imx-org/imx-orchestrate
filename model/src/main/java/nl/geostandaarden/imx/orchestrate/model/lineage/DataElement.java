package nl.geostandaarden.imx.orchestrate.model.lineage;

public interface DataElement {

    ObjectReference getSubject();

    String getProperty();

    Object getValue();
}
