import json

def double_uv(s):
  with open(s) as f:
      raw = f.read()
      j = json.loads(raw)
      for entry in j.get("elements"):
          faces = entry["faces"]
          for key in faces:
              faces[key]["uv"] = [i*2 for i in faces[key]["uv"]] 

      with open(s, "w") as newf:
          newf.write(json.dumps(j, indent=4))

def scale_uv_up(a):
  return [16*i for i in a]

def scale_uv_down(a):
  return [i/16 for i in a]
