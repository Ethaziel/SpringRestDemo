package cz.psgs.SpringRestDemo.util.constants;

public enum Authority {
    USER, // can update and delete self object and read anything
    ADMIN, // can update, delete and read anything
    EDITOR
}
