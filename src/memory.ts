interface MAEMemory {
  outputElem: Element | null,
  logContent: string,
  files: Array<File>
}

const memory: MAEMemory = {
  outputElem: null,
  logContent: "",
  files: []
}

export default memory;