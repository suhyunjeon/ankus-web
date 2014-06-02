/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package org.openflamingo.remote.thrift.thriftfs.api;

import org.apache.commons.lang.builder.HashCodeBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context options for every request.
 */
public class RequestContext implements org.apache.thrift.TBase<RequestContext, RequestContext._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RequestContext");

  private static final org.apache.thrift.protocol.TField CONF_OPTIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("confOptions", org.apache.thrift.protocol.TType.MAP, (short)1);

  /**
   * This map turns into a Configuration object in the server and
   * is currently used to construct a UserGroupInformation to
   * authenticate this request.
   */
  public Map<String,String> confOptions;

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * This map turns into a Configuration object in the server and
     * is currently used to construct a UserGroupInformation to
     * authenticate this request.
     */
    CONF_OPTIONS((short)1, "confOptions");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // CONF_OPTIONS
          return CONF_OPTIONS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments

  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.CONF_OPTIONS, new org.apache.thrift.meta_data.FieldMetaData("confOptions", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RequestContext.class, metaDataMap);
  }

  public RequestContext() {
  }

  public RequestContext(
    Map<String,String> confOptions)
  {
    this();
    this.confOptions = confOptions;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public RequestContext(RequestContext other) {
    if (other.isSetConfOptions()) {
      Map<String,String> __this__confOptions = new HashMap<String,String>();
      for (Map.Entry<String, String> other_element : other.confOptions.entrySet()) {

        String other_element_key = other_element.getKey();
        String other_element_value = other_element.getValue();

        String __this__confOptions_copy_key = other_element_key;

        String __this__confOptions_copy_value = other_element_value;

        __this__confOptions.put(__this__confOptions_copy_key, __this__confOptions_copy_value);
      }
      this.confOptions = __this__confOptions;
    }
  }

  public RequestContext deepCopy() {
    return new RequestContext(this);
  }

  @Override
  public void clear() {
    this.confOptions = null;
  }

  public int getConfOptionsSize() {
    return (this.confOptions == null) ? 0 : this.confOptions.size();
  }

  public void putToConfOptions(String key, String val) {
    if (this.confOptions == null) {
      this.confOptions = new HashMap<String,String>();
    }
    this.confOptions.put(key, val);
  }

  /**
   * This map turns into a Configuration object in the server and
   * is currently used to construct a UserGroupInformation to
   * authenticate this request.
   */
  public Map<String,String> getConfOptions() {
    return this.confOptions;
  }

  /**
   * This map turns into a Configuration object in the server and
   * is currently used to construct a UserGroupInformation to
   * authenticate this request.
   */
  public RequestContext setConfOptions(Map<String,String> confOptions) {
    this.confOptions = confOptions;
    return this;
  }

  public void unsetConfOptions() {
    this.confOptions = null;
  }

  /** Returns true if field confOptions is set (has been assigned a value) and false otherwise */
  public boolean isSetConfOptions() {
    return this.confOptions != null;
  }

  public void setConfOptionsIsSet(boolean value) {
    if (!value) {
      this.confOptions = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case CONF_OPTIONS:
      if (value == null) {
        unsetConfOptions();
      } else {
        setConfOptions((Map<String,String>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CONF_OPTIONS:
      return getConfOptions();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case CONF_OPTIONS:
      return isSetConfOptions();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof RequestContext)
      return this.equals((RequestContext)that);
    return false;
  }

  public boolean equals(RequestContext that) {
    if (that == null)
      return false;

    boolean this_present_confOptions = true && this.isSetConfOptions();
    boolean that_present_confOptions = true && that.isSetConfOptions();
    if (this_present_confOptions || that_present_confOptions) {
      if (!(this_present_confOptions && that_present_confOptions))
        return false;
      if (!this.confOptions.equals(that.confOptions))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_confOptions = true && (isSetConfOptions());
    builder.append(present_confOptions);
    if (present_confOptions)
      builder.append(confOptions);

    return builder.toHashCode();
  }

  public int compareTo(RequestContext other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    RequestContext typedOther = (RequestContext)other;

    lastComparison = Boolean.valueOf(isSetConfOptions()).compareTo(typedOther.isSetConfOptions());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetConfOptions()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.confOptions, typedOther.confOptions);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    org.apache.thrift.protocol.TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == org.apache.thrift.protocol.TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // CONF_OPTIONS
          if (field.type == org.apache.thrift.protocol.TType.MAP) {
            {
              org.apache.thrift.protocol.TMap _map4 = iprot.readMapBegin();
              this.confOptions = new HashMap<String,String>(2*_map4.size);
              for (int _i5 = 0; _i5 < _map4.size; ++_i5)
              {
                String _key6;
                String _val7;
                _key6 = iprot.readString();
                _val7 = iprot.readString();
                this.confOptions.put(_key6, _val7);
              }
              iprot.readMapEnd();
            }
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.confOptions != null) {
      oprot.writeFieldBegin(CONF_OPTIONS_FIELD_DESC);
      {
        oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, this.confOptions.size()));
        for (Map.Entry<String, String> _iter8 : this.confOptions.entrySet())
        {
          oprot.writeString(_iter8.getKey());
          oprot.writeString(_iter8.getValue());
        }
        oprot.writeMapEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("RequestContext(");
    boolean first = true;

    sb.append("confOptions:");
    if (this.confOptions == null) {
      sb.append("null");
    } else {
      sb.append(this.confOptions);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

}

