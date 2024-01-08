package com.brokencircuits.animatedflow.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyEditorSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class GenericPropertyEditor<T> extends PropertyEditorSupport {

  private final ObjectMapper objectMapper;
  private final Class<T> forClass;

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    if (StringUtils.isEmpty(text)) {
      setValue(null);
    } else {
      try {
        T parsed = objectMapper.readValue(text, forClass);
        setValue(parsed);
      } catch (JsonProcessingException e) {
        throw new IllegalStateException(e);
      }
    }
  }

}