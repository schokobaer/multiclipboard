# Multicliplboard

### Build
```
mvn clean compile assembly:single
```

### Usage

The Multiclipboard has a storage of 12 slots. After copying something to the clipboard
it will take the next free slot in the storage.

`Ctrl` + `Shift` + `|` will list all the used slots with their value.

`Ctrl` + `Shift` + `Backspace` will remove the latest item from the storage.

`Ctrl` + `Shift` + [`F1`-`F12`] will paste the item of the given slot and also set the clipboard's value to that item.

Use `Ctrl` + `C` in the CLI to exit the application.