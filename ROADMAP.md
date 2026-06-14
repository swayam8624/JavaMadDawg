# Engine Modernization & Refactoring Roadmap

This roadmap details the future modernization and refactoring path to transition the **Rain Remastered** codebase from software rendering to modern desktop platforms (C++, Vulkan, LWJGL).

---

## Phase 1: Java2D Software (Current)
* [x] Precise Fixed-Timestep loop
* [x] Software blitting backbuffer
* [x] ECS-Lite compositional model
* [x] A* pathfinding & JSON serialization
* [x] Automated unit test suite

## Phase 2: Hardware Acceleration (LWJGL / OpenGL)
* [ ] Integrate **LWJGL 3** library (OpenGL bindings)
* [ ] Implement an `OpenGLRenderContext` utilizing vertex shaders and textures
* [ ] Convert software `Sprite` pixel buffers to GPU texture bindings (`glBindTexture`)
* [ ] Replace canvas blitting with a GPU quad-renderer
* [ ] Measure performance changes (FPS target)

## Phase 3: Transition to C++ & SDL2
* [ ] Translate core math (`Vector2f`, `Vector2i`) to C++ classes
* [ ] Implement application wrapper using **SDL2** for windowing and input handling
* [ ] Rewrite game loop in C++ using high-resolution performance counters (`std::chrono::high_resolution_clock`)
* [ ] Port composition entity systems (replacing Java generic checks with C++ runtime type information or template helpers)
* [ ] Build a software rendering backbuffer in C++ using `SDL_UpdateTexture` and `SDL_RenderCopy`

## Phase 4: Porting to Vulkan & C++
* [ ] Set up Vulkan SDK environment
* [ ] Write standard Vulkan initialization code (Vulkan Instance, Physical/Logical Devices, Queue Families)
* [ ] Build Vulkan Swapchain, Render Pass, Framebuffers
* [ ] Port rendering pipeline to GLSL vertex/fragment shaders compiled to SPIR-V
* [ ] Build Vulkan Descriptor Sets and Pipeline Layout for batch sprite drawing (Instanced Rendering)
* [ ] Implement thread-safe command buffer generation matching Java's modular level structures
