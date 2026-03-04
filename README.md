
DynamicIE
=======
DynamicIE extends the player inventory in a configurable and extensible way.

DynamicIE was built with mod support in mind, meaning very little of its code targets specific containers.
On apper, this means that any mod that *properly* implements a container (assuming they use the vanilla systems) *should* work out the box.

If not, the structure of DynamicIE allows it to easily patch mods that don't work properly.

Details?
======
This mod hacks rendering and user interfaces to make a guess about what something is.
It goes through a baseline set of assumptions to make sure that what its extending is actually an inventory.
It does not inject into specific inventories, or manually change anything about most inventories, and is therefore incredibly hacky and highly experimental.

However, the result is that when it does work, it seamlessly expands the inventories and UIs of mods without ever having to support them individually, achieving the goal of simply adding more inventory space, no strings attached.

Tested With
======
- Create
- Curios
- Accessories (Experimental does not work currently)