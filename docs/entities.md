# Entity Component System (ECS-Lite)

The component-composition architecture allows building flexible game objects without deep subclass inheritance trees.

---

## 1. Creating a Custom Component

All components extend the base class `Component`:
```java
public class CustomComponent extends Component {
    @Override
    public void init() {
        // Run on instantiation
    }

    @Override
    public void update(double deltaTime) {
        // Game tick updates
    }
}
```

---

## 2. Attaching to an Entity

```java
Entity character = new Entity("Soldier", "enemy");
character.addComponent(new TransformComponent(100, 100));
character.addComponent(new CustomComponent());
```

---

## 3. Querying Components

Components can query other components on the owner entity using generics:
```java
TransformComponent transform = owner.getComponent(TransformComponent.class);
if (transform != null) {
    float x = transform.getX();
}
```
This keeps the system decoupled and type-safe.
