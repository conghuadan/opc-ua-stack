package com.inductiveautomation.opcua.stack.core.types.structured;

import com.inductiveautomation.opcua.stack.core.Identifiers;
import com.inductiveautomation.opcua.stack.core.serialization.DelegateRegistry;
import com.inductiveautomation.opcua.stack.core.serialization.UaDecoder;
import com.inductiveautomation.opcua.stack.core.serialization.UaEncoder;
import com.inductiveautomation.opcua.stack.core.types.builtin.LocalizedText;
import com.inductiveautomation.opcua.stack.core.types.builtin.NodeId;
import com.inductiveautomation.opcua.stack.core.types.builtin.QualifiedName;
import com.inductiveautomation.opcua.stack.core.types.builtin.Variant;
import com.inductiveautomation.opcua.stack.core.types.enumerated.NodeClass;

public class VariableNode extends InstanceNode {

    public static final NodeId TypeId = Identifiers.VariableNode;
    public static final NodeId BinaryEncodingId = Identifiers.VariableNode_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.VariableNode_Encoding_DefaultXml;

    protected final Variant _value;
    protected final NodeId _dataType;
    protected final Integer _valueRank;
    protected final Long[] _arrayDimensions;
    protected final Short _accessLevel;
    protected final Short _userAccessLevel;
    protected final Double _minimumSamplingInterval;
    protected final Boolean _historizing;

    public VariableNode(NodeId _nodeId, NodeClass _nodeClass, QualifiedName _browseName, LocalizedText _displayName, LocalizedText _description, Long _writeMask, Long _userWriteMask, ReferenceNode[] _references, Variant _value, NodeId _dataType, Integer _valueRank, Long[] _arrayDimensions, Short _accessLevel, Short _userAccessLevel, Double _minimumSamplingInterval, Boolean _historizing) {
        super(_nodeId, _nodeClass, _browseName, _displayName, _description, _writeMask, _userWriteMask, _references);
        this._value = _value;
        this._dataType = _dataType;
        this._valueRank = _valueRank;
        this._arrayDimensions = _arrayDimensions;
        this._accessLevel = _accessLevel;
        this._userAccessLevel = _userAccessLevel;
        this._minimumSamplingInterval = _minimumSamplingInterval;
        this._historizing = _historizing;
    }

    public Variant getValue() { return _value; }

    public NodeId getDataType() { return _dataType; }

    public Integer getValueRank() { return _valueRank; }

    public Long[] getArrayDimensions() { return _arrayDimensions; }

    public Short getAccessLevel() { return _accessLevel; }

    public Short getUserAccessLevel() { return _userAccessLevel; }

    public Double getMinimumSamplingInterval() { return _minimumSamplingInterval; }

    public Boolean getHistorizing() { return _historizing; }

    @Override
    public NodeId getTypeId() { return TypeId; }

    @Override
    public NodeId getBinaryEncodingId() { return BinaryEncodingId; }

    @Override
    public NodeId getXmlEncodingId() { return XmlEncodingId; }


    public static void encode(VariableNode variableNode, UaEncoder encoder) {
        encoder.encodeNodeId("NodeId", variableNode._nodeId);
        encoder.encodeSerializable("NodeClass", variableNode._nodeClass);
        encoder.encodeQualifiedName("BrowseName", variableNode._browseName);
        encoder.encodeLocalizedText("DisplayName", variableNode._displayName);
        encoder.encodeLocalizedText("Description", variableNode._description);
        encoder.encodeUInt32("WriteMask", variableNode._writeMask);
        encoder.encodeUInt32("UserWriteMask", variableNode._userWriteMask);
        encoder.encodeArray("References", variableNode._references, encoder::encodeSerializable);
        encoder.encodeVariant("Value", variableNode._value);
        encoder.encodeNodeId("DataType", variableNode._dataType);
        encoder.encodeInt32("ValueRank", variableNode._valueRank);
        encoder.encodeArray("ArrayDimensions", variableNode._arrayDimensions, encoder::encodeUInt32);
        encoder.encodeByte("AccessLevel", variableNode._accessLevel);
        encoder.encodeByte("UserAccessLevel", variableNode._userAccessLevel);
        encoder.encodeDouble("MinimumSamplingInterval", variableNode._minimumSamplingInterval);
        encoder.encodeBoolean("Historizing", variableNode._historizing);
    }

    public static VariableNode decode(UaDecoder decoder) {
        NodeId _nodeId = decoder.decodeNodeId("NodeId");
        NodeClass _nodeClass = decoder.decodeSerializable("NodeClass", NodeClass.class);
        QualifiedName _browseName = decoder.decodeQualifiedName("BrowseName");
        LocalizedText _displayName = decoder.decodeLocalizedText("DisplayName");
        LocalizedText _description = decoder.decodeLocalizedText("Description");
        Long _writeMask = decoder.decodeUInt32("WriteMask");
        Long _userWriteMask = decoder.decodeUInt32("UserWriteMask");
        ReferenceNode[] _references = decoder.decodeArray("References", decoder::decodeSerializable, ReferenceNode.class);
        Variant _value = decoder.decodeVariant("Value");
        NodeId _dataType = decoder.decodeNodeId("DataType");
        Integer _valueRank = decoder.decodeInt32("ValueRank");
        Long[] _arrayDimensions = decoder.decodeArray("ArrayDimensions", decoder::decodeUInt32, Long.class);
        Short _accessLevel = decoder.decodeByte("AccessLevel");
        Short _userAccessLevel = decoder.decodeByte("UserAccessLevel");
        Double _minimumSamplingInterval = decoder.decodeDouble("MinimumSamplingInterval");
        Boolean _historizing = decoder.decodeBoolean("Historizing");

        return new VariableNode(_nodeId, _nodeClass, _browseName, _displayName, _description, _writeMask, _userWriteMask, _references, _value, _dataType, _valueRank, _arrayDimensions, _accessLevel, _userAccessLevel, _minimumSamplingInterval, _historizing);
    }

    static {
        DelegateRegistry.registerEncoder(VariableNode::encode, VariableNode.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(VariableNode::decode, VariableNode.class, BinaryEncodingId, XmlEncodingId);
    }

}