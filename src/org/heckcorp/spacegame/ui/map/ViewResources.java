package org.heckcorp.spacegame.ui.map;

import com.google.common.collect.Maps;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Map;

public class ViewResources {
  public record Identifier() {}

  public Identifier addImageResource(InputStream inputStream) {
    Identifier identifier = new Identifier();
    images.put(identifier, new Image(inputStream));
    return identifier;
  }

  public Map<Identifier, Image> getImages() {
    return images;
  }

  private final Map<Identifier, Image> images = Maps.newHashMap();
}
