export default class MAEDirectory {
  name: string = ``;
  files: Array<File> = [];

  constructor(name?: string, files?: Array<File>) {
    if (name) this.name = name;
    if (files) this.files = files;
  }
}