import { MAEFile } from './file-processor'

interface MAEMemory {
  outputElem: Element | null,
  logContent: string,
  files: Array<MAEFile>
}

const memory: MAEMemory = {
  outputElem: null,
  logContent: "",
  files: []
}

export default memory