package com.digitalpetri.opcua.stack.core.types.builtin;

import com.google.common.base.MoreObjects;

public final class XmlElement {

    private final String fragment;

    public XmlElement(String fragment) {
        this.fragment = fragment;
    }

    public String getFragment() {
        return fragment;
    }

    public static XmlElement of(String fragment) {
        return new XmlElement(fragment);
    }

    public boolean isNull() {
        return fragment == null;
    }

    public boolean isNotNull() {
        return !isNull();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XmlElement that = (XmlElement) o;

        return !(fragment != null ? !fragment.equals(that.fragment) : that.fragment != null);

    }

    @Override
    public int hashCode() {
        return fragment != null ? fragment.hashCode() : 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("fragment", fragment).toString();
    }

}
