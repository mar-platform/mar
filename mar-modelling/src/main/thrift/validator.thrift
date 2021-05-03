namespace java mar.analysis.thrift

struct ValidationJob {
  1: string modelId,
  2: string relative_path,
  3: string full_path,
  4: string type,
  5: map<string, string> options,
}

struct Result {
  1: string status,
  2: map<string,i32> stats,
  3: map<string,list<string>> metadata,
  4: optional string metadata_json
}

exception InvalidOperation {
  1: string why
}

service ValidateService {

	Result validate(1:ValidationJob job) throws (1:InvalidOperation ouch)

}