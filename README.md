
DynamicIE
=======
DynamicIE extends the player inventory in a configurable and extensible way.

DynamicIE was built with mod support in mind, meaning very little of its code targets specific containers.
On apper, this means that any mod that *properly* implements a container (assuming they use the vanilla systems) *should* work out the box.

If not, the structure of DynamicIE allows it to easily patch mods that don't work properly.