package br.unifor.kubow.model.acme.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeModelOperation;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.substringBetween;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public abstract class KubowPropertyCommand extends AcmeModelOperation<IAcmeProperty> {

  private final String componentType;
  private final String propertyName;
  private final ObjectMapper mapper;
  private final JsonNode rawValue;

  public KubowPropertyCommand(
      String componentType,
      String propertyName,
      AcmeModelInstance model,
      String target,
      String value)
      throws IOException {
    super("set" + substringBetween(propertyName, "<", ">"), model, target, value);
    this.componentType = componentType;
    this.propertyName = substringBetween(propertyName, "<", ">");
    this.mapper = new ObjectMapper();
    rawValue = mapper.readTree(value);
  }

  protected ObjectMapper getMapper() {
    return mapper;
  }

  protected Object extractValue(JsonNode rawValue) {
    var value = rawValue.get(getPropertyName());
    if (value == null) {
      throw new IllegalArgumentException(
          "Cannot convert Json object of property " + getPropertyName() + " to an Java type");
    }
    if (value.isBoolean()) {
      return value.booleanValue();
    } else if (value.isDouble()) {
      return value.doubleValue();
    } else if (value.isShort()) {
      return value.shortValue();
    } else if (value.isFloat()) {
      return value.floatValue();
    } else if (value.isInt()) {
      return value.intValue();
    } else if (value.isTextual()) {
      return value.textValue();
    } else {
      throw new IllegalArgumentException(
          "Cannot convert Json object of type " + value.getNodeType().name() + " to an Java type");
    }
  }

  protected String getComponentType() {
    return componentType;
  }

  protected String getPropertyName() {
    return propertyName;
  }

  protected IAcmePropertyValue getAcmeValue(Object value) {
    return PropertyHelper.toAcmeVal(value);
  }

  @Override
  protected List<IAcmeCommand<?>> doConstructCommand() throws RainbowModelException {
    var target = getModelContext().resolveInModel(getTarget(), IAcmeComponent.class);
    if (target == null) {
      throw new RainbowModelException(
          MessageFormat.format(
              "The {0} ''{1}'' could not be found in the model", getComponentType(), getTarget()));
    }
    if (!target.declaresType(getComponentType())) {
      throw new RainbowModelException(
          MessageFormat.format(
              "The {0} ''{1}'' is not of the right type. It does not have a property ''{2}''",
              getComponentType(), getTarget(), propertyName));
    }
    var value = extractValue(rawValue);
    var property = target.getProperty(getPropertyName());
    var acmeValue = getAcmeValue(value);
    if (propertyValueChanging(property, acmeValue)) {
      m_command = target.getCommandFactory().propertyValueSetCommand(property, acmeValue);
      List<IAcmeCommand<?>> list = new LinkedList<>();
      list.add(m_command);
      return list;
    }
    return emptyList();
  }

  @Override
  public IAcmeProperty getResult() throws IllegalStateException {
    if (m_command == null) {
      return null;
    }
    return ((IAcmePropertyCommand) m_command).getProperty();
  }

  @Override
  protected boolean checkModelValidForCommand(IAcmeSystem acmeSystem) {
    return acmeSystem.declaresType("KubernetesFam");
  }
}
