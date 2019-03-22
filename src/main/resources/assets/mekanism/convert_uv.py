import json

def double_uv(s, doX, doY):
  with open(s) as f:
      raw = f.read()
      j = json.loads(raw)
      for entry in j.get("elements"):
          faces = entry["faces"]
          for key in faces:
              coords = faces[key]["uv"] 
              if doX:
                  coords[0] = 2*coords[0]
                  coords[2] = 2*coords[2]
              if doY:
                  coords[1] = 2*coords[1]
                  coords[3] = 2*coords[3]
              faces[key]["uv"] = coords


      with open(s, "w") as newf:
          newf.write(json.dumps(j, indent=4))

def scale_uv_up(a):
  return [16*i for i in a]

def scale_uv_down(a):
  return [i/16 for i in a]
