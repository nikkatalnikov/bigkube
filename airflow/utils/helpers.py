import yaml


def yaml_2_json(yaml_file):
    """
    :param yaml_file: file object
    :return: data as dict format
    """
    with open(yaml_file, 'r') as stream:
        try:
            data = yaml.load(stream)
        except Exception:
            raise
    return data
