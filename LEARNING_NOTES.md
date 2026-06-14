# Engine Architect Learning Notes

This document summarizes the core architectural insights, takeaways, and lessons learned from analyzing and rebuilding The Cherno's Java Game Programming project.

---

## 1. The Power of Software Rendering
* **Takeaway**: Directly manipulating a 1D pixel array (`int[] pixels`) forces you to understand exactly how buffers are structured in video memory.
* **Math Insight**: Calculating 2D indices on a flat 1D array is a fundamental calculation in software rendering:
  $$\text{Index} = x + y \times \text{Width}$$
* **Application**: This mapping is identical to texture memory mappings in modern APIs like OpenGL, DirectX, and Vulkan.

---

## 2. Decoupled Interface Boundaries
* **Insight**: Modular projects enforce clean coding. By forcing math, renderer, and logic into separate Gradle modules, we prevent circular dependencies and high class coupling.
* **Takeaway**: Designing a rendering system with a generic `RenderContext` means the game logic is completely unaware of the rendering backend (software vs GPU). This interface-driven layout allows swapping backends seamlessly.

---

## 3. ECS composition over Inheritance hierarchies
* **Takeaway**: Hardcoded inheritance tree hierarchies (e.g., `WizardProjectile` inherits from `Projectile` which inherits from `Entity`) lead to brittle code. A composition model where entities hold components is much more flexible.
* **C++ Connection**: ECS-lite structures easily port to C++ using structs and templates, setting up high-performance layouts (like Array of Structures or Structure of Arrays) suitable for cache-friendly memory access.

---

## 4. UDP Networking & State Synchronization
* **Takeaway**: UDP is fast but unreliable. The server acts as the source of truth, receiving raw input/movement updates and broadcasting state.
* **Future Work**: True multiplayer engines require advanced synchronization features like client-side prediction, entity interpolation, and lag compensation to mask network latency.
