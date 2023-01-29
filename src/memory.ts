import MAEDirectory from './classes/directory';

interface MAEMemory {
  outputElem: Element | null,
  logContent: string,
  files: Array<File | MAEDirectory>
}

const memory: MAEMemory = {
  outputElem: null,
  logContent: "",
  files: []
}

export default memory;